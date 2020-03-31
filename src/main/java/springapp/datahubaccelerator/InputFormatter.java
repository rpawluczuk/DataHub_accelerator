package springapp.datahubaccelerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Service;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.services.InputService;

import java.text.ParseException;
import java.util.Locale;

@Service
public class InputFormatter implements Formatter<Input> {


    @Autowired
    InputService inputService;

    @Override
    public Input parse(String idAsString, Locale locale) throws ParseException {
        return inputService.getLastInput();
    }

    @Override
    public String print(Input input, Locale locale) {
        return input.toString();
    }
}

