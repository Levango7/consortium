package org.wisdom.consortium.consensus.poa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.Block;
import java.util.Date;

@Slf4j
public class MineThread {
    @Getter
    @AllArgsConstructor
    static class MinedResult{
        private boolean success;
        private Block block;
    }
    public static MinedResult pow(Block block, long parentBlockTimeStamp, long endTime) {
        while (true) {
            block.setCreatedAt(System.currentTimeMillis() / 1000);
            if (block.getCreatedAt() <= parentBlockTimeStamp) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
                continue;
            }
            if (block.getCreatedAt() >= endTime) {
                log.error("mining timeout, dead line = " + new Date(endTime * 1000).toString() + "consider upgrade your hardware");
                return new MinedResult(false, block);
            }
            log.info("mining success");
            return new MinedResult(true, block);
        }
    }
}
