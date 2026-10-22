import type { UserProfile } from '@/types/finance';

function profilePhotoUrl(value: unknown): string | null {
  if (typeof value !== 'string') return null;
  const trimmed = value.trim();
  return /^https:\/\/[^\s/]+(?:\/[^\s]*)?$/i.test(trimmed) ? trimmed : null;
}

/** Ignore a late snapshot from an account that has already signed out/switched. */
export function mergeAccountPhoto(
  current: UserProfile | null,
  ownerUid: string,
  savedPhoto: unknown,
  authPhoto: unknown,
): UserProfile | null {
  if (!current || current.uid !== ownerUid) return current;
  const photoURL = profilePhotoUrl(savedPhoto) || profilePhotoUrl(authPhoto);
  return current.photoURL === photoURL ? current : { ...current, photoURL };
}
