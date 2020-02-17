package springapp.datahubaccelerator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.domain.repository.InputRepository;

@Service
public class InputService {

    @Autowired
    InputRepository inputRepository;

    public void saveInput(Input input) {
        inputRepository.saveInput(input);
    }
}
