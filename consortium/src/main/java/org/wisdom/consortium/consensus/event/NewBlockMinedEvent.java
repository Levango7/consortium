package org.wisdom.consortium.consensus.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wisdom.common.Block;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewBlockMinedEvent {

    private Block block;

}
