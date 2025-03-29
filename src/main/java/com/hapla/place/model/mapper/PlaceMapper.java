package com.hapla.place.model.mapper;

import com.hapla.place.model.vo.Place;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlaceMapper {
    int countStar(String placeId);

    int checkPlace(Place place);

    int insertPlace(Place place);

    int deletePlace(Place place);
}
