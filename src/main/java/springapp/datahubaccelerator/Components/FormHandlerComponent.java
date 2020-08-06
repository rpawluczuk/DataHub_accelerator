package springapp.datahubaccelerator.Components;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


@Component
public class FormHandlerComponent {

    private Set<String> SelectedUserStories;

    public FormHandlerComponent() {
    }

    public Set<String> getSelectedUserStories() {
        return SelectedUserStories;
    }

    public void setSelectedUserStories(Set<String> selectedUserStories) {
        SelectedUserStories = selectedUserStories;
    }
}
