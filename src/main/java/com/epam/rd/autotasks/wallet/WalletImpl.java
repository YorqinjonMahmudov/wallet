package com.epam.rd.autotasks.wallet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class WalletImpl implements Wallet {
    List<Obj> objs = new ArrayList<>();//Collections.synchronizedList(new ArrayList<>());
    PaymentLog log;

    public WalletImpl(List<Account> accounts, PaymentLog log) {
        for (int i = 0; i < accounts.size(); i++) {
            objs.add(new Obj(accounts.get(i)));
        }
        this.log = log;
    }


    public void pay(String recipient, long amount) throws Exception {
        for (int i = 0; i < objs.size(); i++)
            if (objs.get(i).can(amount)) {
                objs.get(i).account.pay(amount);
                log.add(objs.get(i).account, recipient, amount);
                return;
            }
        throw new ShortageOfMoneyException(recipient, amount);
    }

    static class Obj {
        public final AtomicLong val;
        public final Account account;

        public Obj(Account account) {
            this.account = account;
            this.val = new AtomicLong(account.balance());
        }

        public boolean can(long amount) {
            final long l = val.get();
            if (l >= amount) {
                final long l1 = val.compareAndExchange(l, l - amount);
                boolean boo = (l1 == l);
                if (!boo && val.get() >= amount)
                    return can(amount);
                return boo;
            }
            return false;
        }

        public String toString() {
            return "{" +
                    "val=" + val +
                    ", acc=" + account +
                    '}';
        }

    }

}
