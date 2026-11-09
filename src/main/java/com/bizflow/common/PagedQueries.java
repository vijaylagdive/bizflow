package com.bizflow.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

public final class PagedQueries {

	private PagedQueries() {
	}

	public static <T> PageResponse<T> loadByIds(Page<T> page, Function<T, Long> idGetter,
			Function<Collection<Long>, List<T>> loader) {
		List<Long> ids = page.getContent().stream().map(idGetter).toList();
		if (ids.isEmpty()) {
			return PageResponse.from(page, List.of());
		}
		Map<Long, T> byId = loader.apply(ids).stream()
				.collect(Collectors.toMap(idGetter, Function.identity(), (first, ignored) -> first));
		List<T> ordered = ids.stream().map(byId::get).filter(Objects::nonNull).toList();
		return PageResponse.from(page, ordered);
	}

}
