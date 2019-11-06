package org.wisdom.consortium.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.wisdom.common.HexBytes;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wisdom.common.StateRepository;
import org.wisdom.consortium.state.Account;


import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
public class EntryController {
    @Autowired
    private StateRepository repository;

    @GetMapping(value = "/hello", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object hello() {
        return "hello".getBytes(UTF_8);
    }

    @GetMapping(value = "/man", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object man() throws Exception {
        return new HexBytes(Hex.decodeHex("ffffffff".toCharArray()));
    }

    @GetMapping(value = "/exception", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object exception() throws RuntimeException {
        throw new RuntimeException("error");
    }

    @GetMapping(value = "/account/{address}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getAccount(@PathVariable String address) throws Exception{
        return repository.getLastConfirmed(address, Account.class);
    }
}
