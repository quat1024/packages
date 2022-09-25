package agency.highlysuspect.packages.platform;

import agency.highlysuspect.packages.net.ActionPacket;

public interface ClientPlatformSupport {
	void sendActionPacket(ActionPacket packet);
}
