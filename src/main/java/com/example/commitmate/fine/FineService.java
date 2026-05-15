package com.example.commitmate.fine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FineService {
    private final FineRepository fr;

    public List<Fine> findAllFine(Integer groupId) {
        List<Fine> fines = fr.findByGroupId(groupId);
        System.out.println("DEBUG: 조회된 벌금 개수 = " + fines.size());
        return fines;
    }


    public FineResponse.GroupFineInfo getFineInfo(Integer id) {
        List<Fine> fineList = findAllFine(id);
        List<Fine> expiredFines = fineList.stream()
                .filter(Fine::isExpired) // Todo의 마감 기한이 지난 것만 필터링
                .collect(Collectors.toList());
        Integer totalAmount = expiredFines.stream()
                .mapToInt(Fine::getAmount)
                .sum();

        return FineResponse.GroupFineInfo.builder()
                .expiredFines(expiredFines)
                .totalFinesAmount(totalAmount)
                .build();

    }
}
