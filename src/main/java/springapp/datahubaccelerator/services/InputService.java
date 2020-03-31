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
        inputRepository.save(input);
    }

    @Transactional
    public List<Input> findAllInputs() {
        return (List<Input>) inputRepository.findAll();
    }

    @Transactional
    public void deleteInput(Input input) {
        inputRepository.delete(input);
    }

    public Input findInputById(Integer id) {
        return inputRepository.findById(id).get();
    }

    @Transactional
    public Input getLastInput() {
        List<Input> allInputsList = findAllInputs();
        int lastId = allInputsList.get(allInputsList.size() - 1).getId();
        return findInputById(lastId);
    }
}
