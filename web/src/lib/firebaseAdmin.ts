import { getApps, initializeApp, cert, getApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { getAuth } from 'firebase-admin/auth';

const projectId =
  process.env.FIREBASE_PROJECT_ID || process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID || 'finlux-d0297';
const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
const privateKey = process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n');

const app = !getApps().length
  ? initializeApp(
      clientEmail && privateKey
        ? {
            credential: cert({
              projectId,
              clientEmail,
              privateKey,
            }),
          }
        : {
            projectId,
          }
    )
  : getApp();

export const adminDb = getFirestore(app);
export const adminAuth = getAuth(app);
export default app;
