package springapp.datahubaccelerator.Components;

import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class FormHandlerComponent {

    private List<String> SelectedUserStories;

    public FormHandlerComponent() {
    }

    public List<String> getSelectedUserStories() {
        return SelectedUserStories;
    }

    public void setSelectedUserStories(List<String> selectedUserStories) {
        SelectedUserStories = selectedUserStories;
    }
}
