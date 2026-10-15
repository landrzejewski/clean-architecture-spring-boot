package pl.fullstackdeveloper.common.proxy;

import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.common.annotations.Atomic;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtomicInterceptorTest {

    interface Operation {

        String run(boolean fail);

        String notAtomic();

    }

    static class AtomicOperation implements Operation {

        @Atomic
        @Override
        public String run(boolean fail) {
            if (fail) {
                throw new IllegalStateException("boom");
            }
            return "done";
        }

        @Override
        public String notAtomic() {
            return "plain";
        }

    }

    static class RecordingTransactionManager implements TransactionManager {

        final List<String> calls = new ArrayList<>();

        @Override
        public void begin() {
            calls.add("begin");
        }

        @Override
        public void commit() {
            calls.add("commit");
        }

        @Override
        public void rollback() {
            calls.add("rollback");
        }

    }

    private final RecordingTransactionManager transactionManager = new RecordingTransactionManager();
    private final Operation operation = Proxies.wrap(Operation.class, new AtomicOperation(), new AtomicInterceptor(transactionManager));

    @Test
    void successful_call_is_committed() {
        assertThat(operation.run(false)).isEqualTo("done");
        assertThat(transactionManager.calls).containsExactly("begin", "commit");
    }

    @Test
    void failed_call_is_rolled_back_and_original_exception_is_rethrown() {
        assertThatThrownBy(() -> operation.run(true)).isInstanceOf(IllegalStateException.class).hasMessage("boom");
        assertThat(transactionManager.calls).containsExactly("begin", "rollback");
    }

    @Test
    void method_without_annotation_is_not_transactional() {
        assertThat(operation.notAtomic()).isEqualTo("plain");
        assertThat(transactionManager.calls).isEmpty();
    }

}
