package de.ipvs.as.mbp.service.messaging.message.reply;


import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;

public abstract class ReplyMessageBody extends DomainMessageBody {
    public static class NoReply extends ReplyMessageBody {}
}
