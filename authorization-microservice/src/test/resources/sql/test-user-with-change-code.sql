insert into users(id, login, password, name, email, recovery_code)
values ('4649df5e-f107-4678-868c-17cfe75e845e', 'kamegatze',
        '$2a$10$02CelX7CVipSqnr2JCg18OoWvE.tvj7EDGZl0nvBHN2Tsc/Fir7UC',
        'Иван Иванов',
        'ivan@yandex.ru',
        '3f193465-ee41-41d0-a1ee-9477fdfb302f');

insert into users_authority(id, user_id, authority_id)
values ('bb16cb46-cb27-4fa7-bfd2-32be4f795c46', '4649df5e-f107-4678-868c-17cfe75e845e', '3d777092-9793-11ee-b9d1-0242ac120002');