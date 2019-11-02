package com.ckcclc.fabric.common;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * This class is to check parameter requirements in request arguments
 * @see com.google.common.base.Predicates
 */
public class ParamChecker {

    private PredicateHolder holderHead;
    private PredicateHolder holderTail;

    private ParamChecker() {
        this.holderHead = new PredicateHolder();
        this.holderTail = this.holderHead;
    }

    public static ParamChecker newInstance() {
        return new ParamChecker();
    }

    public static <T> Predicate<T> and(Predicate<? super T>... components) {
        if (components.length == 0) return null;
        Predicate predicate = components[0];
        for (int i = 1; i < components.length; i++) {
            predicate = predicate.and(components[i]);
        }
        return predicate;
    }

    public static <T> Predicate<T> or(Predicate<? super T>... components) {
        if (components.length == 0) return null;
        Predicate predicate = components[0];
        for (int i = 1; i < components.length; i++) {
            predicate = predicate.or(components[i]);
        }
        return predicate;
    }

    public static <T> Predicate<T> nonNull() {
        return Objects::nonNull;
    }

    public static <T> Predicate<T> isNull() {
        return Objects::isNull;
    }

    public static <T> Predicate<T> in(Collection<T> collection) {
        return input -> collection != null && collection.contains(input);
    }

    public static <T> Predicate<T> onCondition(Predicate<T> predicate, Boolean condition) {
        return input -> {
            if (condition != null && condition) {
                return predicate.test(input);
            }
            return true;
        };
    }

    public static class StringChecker {
        public static Predicate<String> isNotBlank() {
            return StringUtils::isNotBlank;
        }

        public static Predicate<String> isNotEmpty() {
            return StringUtils::isNotEmpty;
        }

        public static Predicate<String> endsWith(String suffix) {
            return input -> input != null && input.endsWith(suffix);
        }
    }

    public static class IntChecker {
        public static Predicate<Integer> atLeast(Integer lowerBound) {
            return input -> input != null && input >= lowerBound;
        }

        public static Predicate<Integer> atMost(Integer upperBound) {
            return input -> input != null && input <= upperBound;
        }
    }

    public static class LongChecker {
        public static Predicate<Long> equals(Long other) {
            return input -> input != null && input.equals(other);
        }

        public static Predicate<Long> greaterThan(Long lowerBound) {
            return input -> input != null && input > lowerBound;
        }
    }

    public static class BooleanChecker {
        public static Predicate<Boolean> isFalse() {
            return input -> input != null && !input;
        }
    }

    public static class ListChecker {
        public static Predicate<List> isNotEmpty() {
            return input -> input != null && !input.isEmpty();
        }
    }

    public <T> ParamChecker add(T object, Predicate<T> predicate) {
        return this.add(object, predicate, null);
    }

    public <T> ParamChecker add(T object, Predicate<T> predicate, String errorMsg) {
        return this.addHolder(object, predicate, errorMsg);
    }

    /**
     * check parameters
     * @return true if check parameters pass
     */
    public Result check() {
        PredicateHolder holder = holderHead;
        while (holder != null) {
            if (holder.predicate != null && !holder.predicate.test(holder.object)) {
                return new Result(holder.errorMsg);
            }
            holder = holder.next;
        }
        return new Result();
    }

    private PredicateHolder addHolder() {
        PredicateHolder holder = new PredicateHolder();
        this.holderTail = this.holderTail.next = holder;
        return holder;
    }

    private <T> ParamChecker addHolder(T object, Predicate predicate, String errorMsg) {
        PredicateHolder holder = this.addHolder();
        holder.object = object;
        holder.errorMsg = errorMsg;
        holder.predicate = predicate;
        return this;
    }

    private final class PredicateHolder<T> {
        T object;
        String errorMsg;
        Predicate predicate;
        PredicateHolder next;

        private PredicateHolder() {
        }
    }

    public class Result {
        boolean valid;
        String errorMsg;

        Result() {
            this.valid = true;
        }

        Result(String errorMsg) {
            this.valid = false;
            this.errorMsg = errorMsg;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }

}

