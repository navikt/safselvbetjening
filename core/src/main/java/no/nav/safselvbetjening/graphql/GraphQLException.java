package no.nav.safselvbetjening.graphql;

import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GraphQLException extends RuntimeException {
    GraphQLError error;

    public DataFetcherResult<Object> toDataFetcherResult() {
        return DataFetcherResult.newResult().error(error).build();
    }

    public static GraphQLException of(ErrorCode code, DataFetchingEnvironment env, String message) {
        return new GraphQLException(code.construct(env, message));
    }
}
