package zzik2.soop4j.chat.event;

import java.util.Collections;
import java.util.List;

public class ViewerEvent extends BaseChatEvent {

    private final List<String> userIds;

    public ViewerEvent(List<String> userIds) {
        super();
        this.userIds = Collections.unmodifiableList(userIds);
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public boolean isSingleViewer() {
        return userIds.size() == 1;
    }
}
