package com.mabsplace.mabsplaceback.utils;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public class PaginationUtils {
    private PaginationUtils() {
    }

    public static <E, D> PageDto<D> convertEntityPageToDtoPage(Page<E> entityPage, List<D> alreadyMappedPageContent) {
        PageDto<D> pageDto = constructPageDto(entityPage);
        pageDto.setData(alreadyMappedPageContent);
        return pageDto;
    }

    public static <E, D> PageDto<D> convertEntityPageToDtoPage(Page<E> entityPage, Function<List<E>, List<D>> mappingFunction) {
        PageDto<D> pageDto = constructPageDto(entityPage);
        pageDto.setData((List)mappingFunction.apply(entityPage.getContent()));
        return pageDto;
    }

    private static <E, D> PageDto<D> constructPageDto(Page<E> entityPage) {
        PageDto<D> pageDto = new PageDto();
        pageDto.setPage(entityPage.getNumber() == 0 ? 1 : entityPage.getNumber() + 1);
        pageDto.setLimit(entityPage.getSize());
        pageDto.setTotalPages(entityPage.getTotalPages());
        pageDto.setTotalItems(entityPage.getTotalElements());
        return pageDto;
    }
}

