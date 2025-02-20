package com.lec.packages.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.lec.packages.domain.exercise_code_table;

@Component
public class StringToExerciseCodeConverter implements Converter<String, exercise_code_table> {
    
    @Override
    public exercise_code_table convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        // 여기서 적절히 데이터 생성
        return new exercise_code_table(source, null); // EXERCISE_CODE만 세팅
    }
}

