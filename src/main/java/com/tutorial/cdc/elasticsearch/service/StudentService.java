package com.tutorial.cdc.elasticsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorial.cdc.elasticsearch.entity.Student;
import com.tutorial.cdc.elasticsearch.repository.StudentRepository;
//import com.tutorial.cdc.utils.Operation;
import io.debezium.data.Envelope.Operation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service interface that masks the caller from the implementation that fetches
 * / acts on Student related data.
 *
 * @author Sohan
 */
@Service
@AllArgsConstructor
public class StudentService {

    /**
     * Handle to ElasticSearch
     */
    private final StudentRepository studentRepository;

    /**
     * Updates/Inserts/Delete student data.
     *
     * @param studentData: Student Data
     * @param operation:   Operation
     */
    @Transactional
    public void maintainReadModel(Map<String, Object> studentData, Operation operation) {
        final ObjectMapper mapper = new ObjectMapper();
        final Student student = mapper.convertValue(studentData, Student.class);

        if (Operation.DELETE.name().equals(operation.name())) {
            studentRepository.deleteById(student.getId());
        } else {
            studentRepository.save(student);
        }
    }
}