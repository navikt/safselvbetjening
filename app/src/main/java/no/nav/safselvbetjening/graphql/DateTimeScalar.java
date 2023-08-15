package no.nav.safselvbetjening.graphql;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Locale;

import static java.time.temporal.ChronoUnit.SECONDS;

final class DateTimeScalar {

	static final GraphQLScalarType DATE_TIME = GraphQLScalarType.newScalar()
			.name("DateTime")
			.description("Identifikasjon av dato og tidspunkt etter ISO-8601 standarden.")
			.coercing(new Coercing<>() {
				@Override
				public @Nullable Object serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingSerializeException {
					if (dataFetcherResult instanceof LocalDateTime ldt) {
						return ldt.truncatedTo(SECONDS).toString();
					}
					throw new CoercingSerializeException("Serialisering av " + dataFetcherResult.getClass() + " til " + DATE_TIME.getName() + " er ikke implementert.");
				}

				@Override
				public @Nullable Object parseValue(@NotNull Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseValueException {
					throw new CoercingParseValueException("Parsing av query variabel " + input.getClass() + " til " + DATE_TIME.getName() + " er ikke implementert.");
				}

				@Override
				public @Nullable Object parseLiteral(@NotNull Value<?> input, @NotNull CoercedVariables variables, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseLiteralException {
					throw new CoercingParseLiteralException("Parsing av literal " + input.getClass() + " til " + DATE_TIME.getName() + " er ikke implementert.");
				}
			})
			.build();

	private DateTimeScalar() {
		// ingen instansiering
	}
}
