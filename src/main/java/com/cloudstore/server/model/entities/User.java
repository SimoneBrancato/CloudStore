package com.cloudstore.server.model.entities;

public record User (
   String nickname,
   String name,
   String surname,
   String email,
   String password,
   Permission PermissionID
) {}