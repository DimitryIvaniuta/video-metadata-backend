UPDATE users
SET password = '$2a$10$.lOsTK14qr.2SB0S7.Wt.uB0V22KvNhtwtzrptuPq8xcF0WOSd5bq'
WHERE role = 'USER';

UPDATE users
SET password = '$2a$10$aoxiGHlH0yJDqbT2C8DLW.RN2V0VJNxkhKwLd9zvd5c9WZcCRaX52'
WHERE role = 'ADMIN';
