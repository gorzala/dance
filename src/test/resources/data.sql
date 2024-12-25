-- We are configuring three users here
-- user-without-profile@dancier.net
--   -> valid user without a profile, roles ROLE_USER, ROLE_HUMAN
-- user-without-a-profile@dancier.net
--   -> valid user with a profile (just the id), roles ROLE_USER, ROLE_HUMAN
-- admin@dancier.net
--   -> valid user with admin rights without a profile

-- one ordinary user without a profile
INSERT
  INTO users (id, email, password, email_validated )
  VALUES (
    '62ff5258-8976-11ec-b58c-e35f5b1fc926',
    'user-without-profile@dancier.net',
    '$2a$10$GOChyBEqco9m3wZwkh0RqOTwyWq4HmocguPPfEraSgnbmlrM4.Fey',
     true
  );

INSERT
  INTO user_roles (user_id, role_id)
SELECT '62ff5258-8976-11ec-b58c-e35f5b1fc926',
       id
  FROM roles
 WHERE name IN ('ROLE_USER', 'ROLE_HUMAN');

-- one ordinary user with a profile
INSERT
  INTO users (id, email, password, email_validated )
  VALUES (
    '55bbf334-6649-11ed-8f65-5b299f0e161f',
    'user-with-a-profile@dancier.net',
    '$2a$10$GOChyBEqco9m3wZwkh0RqOTwyWq4HmocguPPfEraSgnbmlrM4.Fey',
     true
  );

INSERT
  INTO user_roles (user_id, role_id)
SELECT '55bbf334-6649-11ed-8f65-5b299f0e161f',
       id
  FROM roles
 WHERE name IN ('ROLE_USER', 'ROLE_HUMAN');

INSERT
  INTO dancer(user_id, id, dancer_name, size, birth_date, gender, country, city, longitude, latitude, about_me)
VALUES ('55bbf334-6649-11ed-8f65-5b299f0e161f', '11065e54-664a-11ed-872e-1b1eb88b44b6', 'good_dancer', '178', '2000-11-11', 'MALE', 'GER', 'Dortmund', '7.1075023', '51.4429498', 'Hi');

-- one admin
-- no profile attached
INSERT
  INTO users (id, email, password, email_validated )
  VALUES (
    '21df1a30-89a6-11ec-b4cf-67ea17ff4219',
    'admin@dancier.net',
    '$2a$10$GOChyBEqco9m3wZwkh0RqOTwyWq4HmocguPPfEraSgnbmlrM4.Fey',
     true
  );

INSERT
  INTO user_roles (user_id, role_id)
SELECT '21df1a30-89a6-11ec-b4cf-67ea17ff4219',
       id
  FROM roles
 WHERE name IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_HUMAN');
