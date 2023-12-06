package io.substrait.extendedexpression;

import io.substrait.expression.Expression;
import io.substrait.expression.proto.ExpressionProtoConverter;
import io.substrait.extension.ExtensionCollector;
import io.substrait.proto.AggregateFunction;
import io.substrait.proto.ExpressionReference;
import io.substrait.proto.ExtendedExpression;
import io.substrait.type.proto.TypeProtoConverter;

/** Converts from {@link ExtendedExpression} to {@link ExtendedExpression} */
public class ExtendedExpressionProtoConverter {
  public ExtendedExpression toProto(
      io.substrait.extendedexpression.ExtendedExpression extendedExpression) {

    ExtendedExpression.Builder builder = ExtendedExpression.newBuilder();
    ExtensionCollector functionCollector = new ExtensionCollector();

    final ExpressionProtoConverter expressionProtoConverter =
        new ExpressionProtoConverter(functionCollector, null);

    for (io.substrait.extendedexpression.ExtendedExpression.ExpressionReference
        expressionReference : extendedExpression.getReferredExpressions()) {
      io.substrait.extendedexpression.ExtendedExpression.ExpressionTypeReference expressionType =
          expressionReference.getExpressionType();
      if (expressionType
          instanceof io.substrait.extendedexpression.ExtendedExpression.ExpressionType) {
        io.substrait.proto.Expression expressionProto =
            expressionProtoConverter.visit(
                (Expression.ScalarFunctionInvocation)
                    ((io.substrait.extendedexpression.ExtendedExpression.ExpressionType)
                            expressionType)
                        .getExpression());
        ExpressionReference.Builder expressionReferenceBuilder =
            ExpressionReference.newBuilder()
                .setExpression(expressionProto)
                .addAllOutputNames(expressionReference.getOutputNames());
        builder.addReferredExpr(expressionReferenceBuilder);
      } else if (expressionType
          instanceof io.substrait.extendedexpression.ExtendedExpression.AggregateFunctionType) {
        AggregateFunction measure =
            ((io.substrait.extendedexpression.ExtendedExpression.AggregateFunctionType)
                    expressionType)
                .getMeasure();
        ExpressionReference.Builder expressionReferenceBuilder =
            ExpressionReference.newBuilder()
                .setMeasure(measure.toBuilder())
                .addAllOutputNames(expressionReference.getOutputNames());
        builder.addReferredExpr(expressionReferenceBuilder);
      } else {
        throw new UnsupportedOperationException(
            "Only Expression or Aggregate Function type are supported in conversion to proto Extended Expressions for now");
      }
    }
    builder.setBaseSchema(
        extendedExpression.getBaseSchema().toProto(new TypeProtoConverter(functionCollector)));

    // the process of adding simple extensions, such as extensionURIs and extensions, is handled on
    // the fly
    functionCollector.addExtensionsToExtendedExpression(builder);
    if (extendedExpression.getAdvancedExtension().isPresent()) {
      builder.setAdvancedExtensions(extendedExpression.getAdvancedExtension().get());
    }
    return builder.build();
  }
}
