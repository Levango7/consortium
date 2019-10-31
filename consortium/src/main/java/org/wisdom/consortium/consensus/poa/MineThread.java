/*
 * Copyright (c) [2018]
 * This file is part of the java-wisdomcore
 *
 * The java-wisdomcore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The java-wisdomcore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the java-wisdomcore. If not, see <http://www.gnu.org/licenses/>.
 */

package org.wisdom.consortium.consensus.poa;

import com.google.common.eventbus.EventBus;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.wisdom.common.Block;
import org.wisdom.common.Miner;
import org.wisdom.consortium.consensus.event.NewBlockMinedEvent;
import org.wisdom.util.BigEndian;


import java.security.SecureRandom;
import java.util.Date;

@Component
@Scope("prototype")
public class MineThread {
    private volatile boolean terminated;
    private static final Logger logger = LoggerFactory.getLogger(Miner.class);

    @Autowired
    private ApplicationContext ctx;

    @Async
    public void mine(EventBus eventBus, Block block, long startTime, long endTime) {
        logger.info("start mining at height " + block.getHeight());
        block = pow(block, startTime, endTime);
        terminated = true;
        eventBus.post(new NewBlockMinedEvent(block));
    }

    private Block pow(Block block, long parentBlockTimeStamp, long endTime) {
        while (!terminated) {
            block.setCreatedAt(System.currentTimeMillis() / 1000);
            if (block.getCreatedAt() <= parentBlockTimeStamp) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {

                }
                continue;
            }
            if (block.getCreatedAt() >= endTime) {
                logger.error("mining timeout, dead line = " + new Date(endTime * 1000).toString() + "consider upgrade your hardware");
                return null;
            }
            logger.info("mining success");
            return block;
        }
        logger.info("mining terminated");
        return null;
    }

    public void terminate() {
        terminated = true;
    }

    boolean isTerminated() {
        return terminated;
    }
}
