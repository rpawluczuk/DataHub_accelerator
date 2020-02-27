package springapp.datahubaccelerator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.domain.repository.InputRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class InputService {

    @Autowired
    InputRepository inputRepository;

    public void saveInput(Input input) {
        inputRepository.saveInput(input);
    }

    public Input getLastInput() {
        return inputRepository.getLastInput();
    }

    public List<Input> getAllInputs() {
        return inputRepository.getAllInputs();
    }

    public void deleteInput(Input input) {
        inputRepository.deleteInput(input);
    }
}
