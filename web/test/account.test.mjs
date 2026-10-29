import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import vm from 'node:vm';
import ts from 'typescript';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { loadModule, webRoot } from './helpers/loadModule.mjs';

const user = { uid: 'qa-user', displayName: 'Tài khoản kiểm thử', email: 'qa@example.invalid', photoURL: null };
const { mergeAccountPhoto } = loadModule('src/lib/accountProfile.ts');

test('saved profile picture takes priority over the Auth/provider picture', () => {
  assert.equal(1, 2, 'Intentional failure to verify Web CI fails as expected');
  const merged = mergeAccountPhoto(user, user.uid, 'https://example.invalid/saved.jpg', 'https://example.invalid/google.jpg');
  assert.equal(merged.photoURL, 'https://example.invalid/saved.jpg');
  assert.equal(merged.uid, user.uid);
  assert.equal(merged.email, user.email);
  assert.equal(user.photoURL, null);
});
test('missing or invalid saved picture falls back to Auth, then to the user icon', () => {
  for (const saved of [undefined, null, '', 'javascript:alert(1)', 'http://example.invalid/photo.jpg', { url: 'not-a-string' }]) {
    assert.equal(mergeAccountPhoto(user, user.uid, saved, 'https://example.invalid/auth.jpg').photoURL, 'https://example.invalid/auth.jpg');
  }
  assert.equal(mergeAccountPhoto(user, user.uid, '', null).photoURL, null);
});
test('late profile updates cannot copy another account photo into the current account', () => {
  assert.equal(mergeAccountPhoto(user, 'previous-user', 'https://example.invalid/other.jpg', null), user);
  assert.equal(mergeAccountPhoto(null, user.uid, 'https://example.invalid/saved.jpg', null), null);
});
test('identical photo snapshots preserve state reference and changed photos propagate', () => {
  const original = { ...user, photoURL: 'https://example.invalid/first.jpg' };
  assert.equal(mergeAccountPhoto(original, user.uid, original.photoURL, null), original);
  assert.equal(mergeAccountPhoto(original, user.uid, '  https://example.invalid/second.jpg  ', null).photoURL, 'https://example.invalid/second.jpg');
});
const icons = {
  FinluxIcon: ({ name }) => React.createElement('svg', { 'data-icon': name }),
  FinluxLogo: () => React.createElement('span', null, 'F'),
};
const image = ({ src, alt, className, onError }) => React.createElement('img', { src, alt, className, onError });
function mocks(finance = {}, extra = {}) {
  return { '@/context/FinanceContext': { useFinance: () => ({
    user, activeTab: 'overview', period: 'month', periods: { month: { name: 'Tháng này' } },
    theme: 'light', isCloudSynced: true, ...finance,
  }) }, '@/components/icons/FinluxIcons': icons, 'next/image': image, ...extra };
}
function find(node, predicate) {
  if (!React.isValidElement(node)) return null;
  if (predicate(node)) return node;
  for (const child of React.Children.toArray(node.props.children)) {
    const match = find(child, predicate);
    if (match) return match;
  }
  return null;
}

test('header shows a compact avatar trigger, not the name or a duplicate dropdown', () => {
  let opened = false;
  const Header = loadModule('src/components/Header.tsx', mocks({ openAccountDetails: () => { opened = true; } })).default;
  const html = renderToStaticMarkup(React.createElement(Header, { onOpenAddTx() {} }));
  assert.match(html, /fx-account-trigger/);
  assert.match(html, /aria-label="Xem thông tin tài khoản"/);
  assert.doesNotMatch(html, /Tài khoản kiểm thử|Profile Dropdown|Đăng xuất/);
  const trigger = find(Header({ onOpenAddTx() {} }), (node) => node.props.className === 'fx-account-trigger');
  trigger.props.onClick();
  assert.equal(opened, true);
});

test('sidebar account card is a keyboard-accessible button using the same account action', () => {
  let opened = false;
  const Sidebar = loadModule('src/components/Sidebar.tsx', mocks({ openAccountDetails: () => { opened = true; } })).default;
  const trigger = find(Sidebar(), (node) => node.props.className === 'fx-side-note');
  assert.equal(trigger.type, 'button');
  assert.equal(trigger.props['aria-label'], 'Xem thông tin tài khoản');
  trigger.props.onClick();
  assert.equal(opened, true);
});

test('guest header keeps the avatar entry available for sign-in', () => {
  const Header = loadModule('src/components/Header.tsx', mocks({ user: null, openAccountDetails() {} })).default;
  const html = renderToStaticMarkup(React.createElement(Header, { onOpenAddTx() {} }));
  assert.match(html, /aria-label="Đăng nhập tài khoản"/);
  assert.match(html, /data-icon="user"/);
  assert.doesNotMatch(html, /fx-demo-chip/);
});

test('avatar shows a supplied HTTPS photo and an icon when missing or unsafe', () => {
  const Avatar = loadModule('src/components/UserAvatar.tsx', mocks()).default;
  assert.match(renderToStaticMarkup(React.createElement(Avatar, { user: { photoURL: 'https://example.invalid/avatar.png' } })), /src="https:\/\/example.invalid\/avatar.png"/);
  for (const photoURL of [null, '', 'javascript:alert(1)', 'http://example.invalid/avatar.png']) {
    const html = renderToStaticMarkup(React.createElement(Avatar, { user: { photoURL } }));
    assert.match(html, /data-icon="user"/);
    assert.doesNotMatch(html, /<img/);
  }
});
test('the real avatar fallback renders a person, not the default dashboard grid', () => {
  const Avatar = loadModule('src/components/UserAvatar.tsx', { 'next/image': image }).default;
  const html = renderToStaticMarkup(React.createElement(Avatar, { user: null }));
  assert.match(html, /<circle cx="12" cy="8" r="4"/);
  assert.doesNotMatch(html, /<rect/);
});

test('failed avatar falls back to the icon and a different photo can load afterward', () => {
  let failedUrl = null;
  const Avatar = loadModule('src/components/UserAvatar.tsx', mocks({}, {
    react: { ...React, useState: () => [failedUrl, (value) => { failedUrl = value; }] },
  })).default;
  const photoUser = { photoURL: 'https://example.invalid/broken.png' };
  const first = Avatar({ user: photoUser });
  first.props.children.props.onError();
  assert.equal(Avatar({ user: photoUser }).props.children.props.name, 'user');
  assert.equal(Avatar({ user: { photoURL: 'https://example.invalid/new.png' } }).props.children.props.src, 'https://example.invalid/new.png');
});

function accountNavigation(currentUser) {
  const file = path.join(webRoot, 'src/context/FinanceContext.tsx');
  const source = fs.readFileSync(file, 'utf8');
  const ast = ts.createSourceFile(file, source, ts.ScriptTarget.Latest, true, ts.ScriptKind.TSX);
  let initializer;
  function visit(node) {
    if (ts.isVariableDeclaration(node) && node.name.getText(ast) === 'openAccountDetails') initializer = node.initializer.getText(ast);
    ts.forEachChild(node, visit);
  }
  visit(ast);
  assert.ok(initializer, 'production account navigation callback exists');
  const state = { drawerOpen: true, activeTab: 'overview', authOpen: false, scroll: null };
  const compiled = ts.transpileModule(`const action = ${initializer}; action;`, { compilerOptions: { target: ts.ScriptTarget.ES2020 } }).outputText;
  const action = vm.runInNewContext(compiled, {
    user: currentUser, useCallback: (callback) => callback,
    setIsSidebarOpen: (value) => { state.drawerOpen = value; },
    setActiveTab: (value) => { state.activeTab = value; },
    setIsAuthModalOpen: (value) => { state.authOpen = value; },
    window: { scrollTo: (value) => { state.scroll = value; } },
  });
  action();
  return state;
}
test('signed-in account action opens existing Settings details and closes the drawer', () => {
  const state = accountNavigation(user);
  assert.equal(state.activeTab, 'settings');
  assert.equal(state.drawerOpen, false);
  assert.equal(state.authOpen, false);
  assert.equal(state.scroll.top, 0);
});
test('guest action opens sign-in without navigating to account details', () => {
  const state = accountNavigation(null);
  assert.equal(state.activeTab, 'overview');
  assert.equal(state.drawerOpen, false);
  assert.equal(state.authOpen, true);
});

test('account detail panel reuses the avatar and shows only the current account data', () => {
  const Settings = loadModule('src/components/views/SettingsView.tsx', mocks()).default;
  const html = renderToStaticMarkup(React.createElement(Settings));
  assert.match(html, /Thông tin tài khoản/);
  assert.match(html, /fx-user-avatar-large/);
  assert.match(html, /qa@example.invalid/);
  assert.match(html, /qa-user/);
});
