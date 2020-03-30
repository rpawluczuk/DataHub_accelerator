package springapp.datahubaccelerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Service;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.domain.repository.InputRepository;

import java.text.ParseException;
import java.util.Locale;

@Service
public class InputFormatter implements Formatter<Input> {


    @Autowired
    InputRepository inputRepository;

    @Override
    public Input parse(String idAsString, Locale locale) throws ParseException {
        return inputRepository.getLastInput();
    }

    @Override
    public String print(Input input, Locale locale) {
        return input.toString();
    }
}

