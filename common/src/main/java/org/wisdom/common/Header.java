package org.wisdom.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Header implements Cloneable<Header>{
    @JsonIgnore
    private long height;

    private long timeStamp;

    @Override
    public Header clone() {
        return builder()
                .height(height)
                .timeStamp(timeStamp).build();
    }
}
