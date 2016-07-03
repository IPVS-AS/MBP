/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.user;

/**
 *
 * @author rafaelkperes
 */
public class User {
    private final UserID id;

    public User(UserID id) {
        this.id = id;
    }

    public UserID getId() {
        return id;
    }
    
}
