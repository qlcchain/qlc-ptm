package com.quorum.tessera.api;

import com.quorum.tessera.config.adapters.MapAdapter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;

@ApiModel
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PayloadDecryptRequest {

    @XmlElement
    @XmlMimeType("base64Binary")
    @ApiModelProperty(value = "The public key of the sender of this payload", dataType = "string")
    private byte[] senderKey;

    @XmlElement
    @XmlMimeType("base64Binary")
    @ApiModelProperty(value = "The payload to be encrypted as encoded as Base64", dataType = "string")
    private byte[] cipherText;

    @XmlElement
    @XmlMimeType("base64Binary")
    @ApiModelProperty(value = "The Base64 encoded nonce used to decrypt that payload with the master key", dataType = "string")
    private byte[] cipherTextNonce;

    @XmlElement
    @XmlMimeType("base64Binary")
    @ApiModelProperty(value = "The encrypted master key, one entry for each recipient", dataType = "string")
    private List<byte[]> recipientBoxes = new ArrayList<>();

    @XmlElement
    @XmlMimeType("base64Binary")
    @ApiModelProperty(value = "The nonce used to encrypt the master key for each recipient", dataType = "string")
    private byte[] recipientNonce;

    @XmlElement
    @XmlMimeType("base64Binary")
    @ApiModelProperty(value = "The recipient public keys", dataType = "string")
    private List<byte[]> recipientKeys = new ArrayList<>();

    @XmlElement
    @ApiModelProperty(value = "The privacy mode of this transaction", allowableValues = "0, 1, 2")
    private int privacyMode;

    @XmlElement
    @XmlJavaTypeAdapter(MapAdapter.class)
    @ApiModelProperty(value = "A list of contracts that are affected by the execution of this transaction", dataType = "string")
    private Map<String, String> affectedContractTransactions = new HashMap<>();

    @XmlElement
    @XmlMimeType("base64Binary")
    @ApiModelProperty(value = "The execution hash resulting from executing the decrypted payload as a private transaction", dataType = "string")
    private byte[] execHash = new byte[0];

    public byte[] getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(final byte[] senderKey) {
        this.senderKey = senderKey;
    }

    public byte[] getCipherText() {
        return cipherText;
    }

    public void setCipherText(final byte[] cipherText) {
        this.cipherText = cipherText;
    }

    public byte[] getCipherTextNonce() {
        return cipherTextNonce;
    }

    public void setCipherTextNonce(final byte[] cipherTextNonce) {
        this.cipherTextNonce = cipherTextNonce;
    }

    public List<byte[]> getRecipientBoxes() {
        return recipientBoxes;
    }

    public void setRecipientBoxes(final List<byte[]> recipientBoxes) {
        this.recipientBoxes = recipientBoxes;
    }

    public byte[] getRecipientNonce() {
        return recipientNonce;
    }

    public void setRecipientNonce(final byte[] recipientNonce) {
        this.recipientNonce = recipientNonce;
    }

    public List<byte[]> getRecipientKeys() {
        return recipientKeys;
    }

    public void setRecipientKeys(final List<byte[]> recipientKeys) {
        this.recipientKeys = recipientKeys;
    }

    public int getPrivacyMode() {
        return privacyMode;
    }

    public void setPrivacyMode(final int privacyMode) {
        this.privacyMode = privacyMode;
    }

    public Map<String, String> getAffectedContractTransactions() {
        return affectedContractTransactions;
    }

    public void setAffectedContractTransactions(final Map<String, String> affectedContractTransactions) {
        this.affectedContractTransactions = affectedContractTransactions;
    }

    public byte[] getExecHash() {
        return execHash;
    }

    public void setExecHash(final byte[] execHash) {
        this.execHash = execHash;
    }
}
