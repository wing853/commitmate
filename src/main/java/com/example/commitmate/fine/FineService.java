package com.example.commitmate.fine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {
    private final FineRepository fr;

    public List<Fine> findAllFine(Integer groupId) {
        List<Fine> fines = fr.findByGroupId(groupId);
        System.out.println("DEBUG: 조회된 벌금 개수 = " + fines.size());
        return fines;
    }

}
