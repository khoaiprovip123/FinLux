'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { FinluxIcon } from '@/components/icons/FinluxIcons';
import type { UserProfile } from '@/types/finance';

interface UserAvatarProps {
  user: Pick<UserProfile, 'photoURL'> | null;
  size?: 'small' | 'large';
}

export default function UserAvatar({ user, size = 'small' }: UserAvatarProps) {
  const [failedUrl, setFailedUrl] = useState<string | null>(null);
  const photoUrl = user?.photoURL;
  const showPhoto = photoUrl && photoUrl !== failedUrl && photoUrl.startsWith('https://');

  return (
    <span className={`fx-user-avatar${size === 'large' ? ' fx-user-avatar-large' : ''}`} aria-hidden="true">
      {showPhoto ? (
        <Image
          src={photoUrl}
          alt=""
          fill
          sizes={size === 'large' ? '64px' : '40px'}
          unoptimized
          referrerPolicy="no-referrer"
          className="fx-user-avatar-image"
          onError={() => setFailedUrl(photoUrl)}
        />
      ) : <FinluxIcon name="user" />}
    </span>
  );
}
