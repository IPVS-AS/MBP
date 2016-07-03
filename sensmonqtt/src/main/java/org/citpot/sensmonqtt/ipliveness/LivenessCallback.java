/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citpot.sensmonqtt.ipliveness;

/**
 *
 * @author rafaelkperes
 */
public interface LivenessCallback {
    
    public void onOnlineDevice(String ip);
    public void onOfflineDevice(String ip);
    
}
