package pro.gravit.sponge.launcherintegration.lk.event.request;

import pro.gravit.launcher.events.RequestEvent;

public class ChangeOrderStatusRequestEvent extends RequestEvent {
    @Override
    public String getType() {
        return "lkChangeOrderStatus";
    }
}
