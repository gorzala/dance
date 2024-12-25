-- 5 test users are added
-- user-with-a-profile@dancier.net
--   -> the user that initiates the query
-- user-matching-criterias@dancier.net
--   -> a user that matches all query parameters and is returned by GET /dancers request
-- user-without-matching-gender@dancier.net
--   -> a user that matches all query parameters except gender and is returned by GET /dancers request
-- user-without-matching-latitude@dancier.net
--   -> valid user with a profile but not matching the latitude
-- user-without-matching-longitude@dancier.net
--   -> valid user with a profile but not matching the longitude

-- user who initiates the query
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

-- one ordinary user with matching profile
INSERT
  INTO users (id, email, password, email_validated )
  VALUES (
    'b8300af3-a27b-41ed-b35b-3735b25a04df',
    'user-matching-criterias@dancier.net',
    '$2a$10$GOChyh6tco9m3wZwkh0RqOTwyWq4HmocguPPfEraSgnbmlrM4.Fax',
     true
  );

INSERT
  INTO user_roles (user_id, role_id)
SELECT 'b8300af3-a27b-41ed-b35b-3735b25a04df',
       id
  FROM roles
 WHERE name IN ('ROLE_USER', 'ROLE_HUMAN');

INSERT
  INTO dancer(user_id, id, dancer_name, size, birth_date, gender, country, city, longitude, latitude, about_me)
VALUES ('b8300af3-a27b-41ed-b35b-3735b25a04df', '503ffad4-148b-4af1-8365-62315ff89b9f', 'perfect_dancer', '178', '1998-11-11', 'FEMALE', 'GER', 'Dortmund', '7.0075023', '51.3429498', 'Hi');

-- one ordinary user with not matching gender
INSERT
  INTO users (id, email, password, email_validated )
  VALUES (
    'b90e6478-19d9-492e-97a8-df1f8f7c3901',
    'user-without-matching-gender@dancier.net',
    '$2a$10$GOChyh6tco9zu6Zwkh0RqOTwyWq4HmocguPPfEraSgnbmlrM4.Fax',
     true
  );

INSERT
  INTO user_roles (user_id, role_id)
SELECT 'b90e6478-19d9-492e-97a8-df1f8f7c3901',
       id
  FROM roles
 WHERE name IN ('ROLE_USER', 'ROLE_HUMAN');

INSERT
  INTO dancer(user_id, id, dancer_name, size, birth_date, gender, country, city, longitude, latitude, about_me)
VALUES ('b90e6478-19d9-492e-97a8-df1f8f7c3901', '0948c9ba-75a9-4821-8701-0cd3e564f10e', 'Horst', '178', '1980-11-11', 'MALE', 'GER', 'Dortmund', '7.0075023', '51.3429498', 'Hi');

-- one ordinary user with not matching latitude
INSERT
  INTO users (id, email, password, email_validated )
  VALUES (
    '6cd96820-ce61-4f72-9fa4-6f8900bb7494',
    'user-without-matching-latitude@dancier.net',
    '$2a$10$GOChyh6tco9m3wZwkk8tqOTwyWq4HmocguPPfEraSgnbmlrM4.Fax',
     true
  );

INSERT
  INTO user_roles (user_id, role_id)
SELECT '6cd96820-ce61-4f72-9fa4-6f8900bb7494',
       id
  FROM roles
 WHERE name IN ('ROLE_USER', 'ROLE_HUMAN');

INSERT
  INTO dancer(user_id, id, dancer_name, size, birth_date, gender, country, city, longitude, latitude, about_me)
VALUES ('6cd96820-ce61-4f72-9fa4-6f8900bb7494', '9593cd4c-bff4-41b5-b7d8-a632a526721a', 'dancer', '178', '1997-11-11', 'FEMALE', 'GER', 'Bremen', '7.0075023', '54.3429498', 'Hi');

-- one ordinary user with not matching longitude
INSERT
  INTO users (id, email, password, email_validated )
  VALUES (
    'e177c7f1-1082-4bd1-ad72-fd88de5b19b2',
    'user-without-matching-longitude@dancier.net',
    '$2a$10$GOChyh6tco9m3wZwkk8tqOTwyWq4HmocguPPfEraSgnbmlrM4.Fax',
     true
  );

INSERT
  INTO user_roles (user_id, role_id)
SELECT 'e177c7f1-1082-4bd1-ad72-fd88de5b19b2',
       id
  FROM roles
 WHERE name IN ('ROLE_USER', 'ROLE_HUMAN');

INSERT
  INTO dancer(user_id, id, dancer_name, size, birth_date, gender, country, city, longitude, latitude, about_me)
VALUES ('e177c7f1-1082-4bd1-ad72-fd88de5b19b2', 'f140bc96-5d65-4e6b-8727-f89f2afef03d', 'dancing_queen', '178', '1995-11-11', 'FEMALE', 'GER', 'Essen', '6.0075023', '51.3429498', 'Hi');
