package app.web;

public class ActionRequest {
    private ActionType action;

    public ActionRequest() {
    }

    public ActionRequest(ActionType action) {
        this.action = action;
    }

    public ActionType getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "ActionRequest{" +
                "action=" + action +
                '}';
    }
}
