/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.user;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;

/**
 *
 * @author rafaelkperes
 */
@Entity("users")
public class User {
    private ObjectId id;

    protected User() {
    }

    public ObjectId getId() {
        return id;
    }
    
}
