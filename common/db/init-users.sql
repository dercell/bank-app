create user acc_admin with password 'acc_admin';
create user cash_admin with password 'cash_admin';
create user notification_admin with password 'notification_admin';
create user transfer_admin with password 'transfer_admin';

GRANT ALL PRIVILEGES ON SCHEMA accounts TO acc_admin;
GRANT ALL PRIVILEGES ON SCHEMA cash TO cash_admin;
GRANT ALL PRIVILEGES ON SCHEMA notification TO notification_admin;
GRANT ALL PRIVILEGES ON SCHEMA transfer TO transfer_admin;
GRANT ALL PRIVILEGES ON SCHEMA keycloak TO keycloak;
