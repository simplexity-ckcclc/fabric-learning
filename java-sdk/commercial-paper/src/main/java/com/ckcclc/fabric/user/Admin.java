package com.ckcclc.fabric.user;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Author:  huangyucong <hyc3@meitu.com>
 * Created: 2019/10/31
 */
@Component(value = "adminClient")
public class Admin implements FactoryBean<HFClient> {

    @Value("${user.admin.name}")
    private String name;

    @Value("${user.admin.msp-id}")
    private String mspId;

    @Value("${user.admin.key-file}")
    private String keyFile;

    @Value("${user.admin.cert-file}")
    private String certFile;


    @Value("${peer.name}")
    private String peerName;

    @Value("${peer.grpc-url}")
    private String peerURL;

    @Value("${peer.tlsca-cert-file}")
    private String peerTLScaCertFile;

    @Value("${orderer.name}")
    private String ordererName;

    @Value("${orderer.grpc-url}")
    private String ordererURL;

    @Value("${orderer.tlsca-cert-file}")
    private String ordererTLScaCertFile;

    @Value("${channel.id}")
    private String channelID;

    private HFClient client;

    public HFClient getObject() throws Exception {
        Enrollment enrollment = PaperUser.createEnrollmentFromPemFile(keyFile, certFile);
        PaperUser admin = new PaperUser(name, mspId, enrollment);

        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(admin);
        this.client = client;
        initChannel();
        return client;
    }

    public Class<?> getObjectType() {
        return HFClient.class;
    }

    public boolean isSingleton() {
        return true;
    }

    private void initChannel() throws Exception {
        Channel channel = client.newChannel(channelID);
        Properties peerProps = new Properties();
        peerProps.put("pemFile", peerTLScaCertFile);
        peerProps.setProperty("sslProvider", "openSSL");
        peerProps.setProperty("negotiationType", "TLS");
        Peer peer = client.newPeer(peerName, peerURL, peerProps);
        channel.addPeer(peer);

        Properties ordererProps = new Properties();
        ordererProps.put("pemFile", ordererTLScaCertFile);
        ordererProps.setProperty("sslProvider", "openSSL");
        ordererProps.setProperty("negotiationType", "TLS");
        Orderer orderer = client.newOrderer(ordererName, ordererURL, ordererProps);
        channel.addOrderer(orderer);

        channel.initialize();
    }

}
