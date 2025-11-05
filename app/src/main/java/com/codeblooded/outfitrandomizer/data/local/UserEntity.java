package com.codeblooded.outfitrandomizer.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String username;

    public String phoneNo;
    public String email;

    public UserEntity(@NonNull String username, String phoneNo, String email) {
        this.username = username;
        this.phoneNo = phoneNo;
        this.email = email;
    }
}
