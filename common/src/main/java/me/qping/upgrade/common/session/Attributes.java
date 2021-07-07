package me.qping.upgrade.common.session;

import io.netty.util.AttributeKey;
import me.qping.upgrade.common.session.Session;

public interface Attributes {

	AttributeKey<Session> SESSION = AttributeKey.newInstance("session");

}
