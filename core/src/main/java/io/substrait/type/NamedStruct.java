package io.substrait.type;

import io.substrait.type.proto.ProtoTypeConverter;
import io.substrait.type.proto.TypeProtoConverter;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface NamedStruct {
  Type.Struct struct();

  List<String> names();

  static NamedStruct of(Iterable<String> names, Type.Struct type) {
    return ImmutableNamedStruct.builder().addAllNames(names).struct(type).build();
  }

  default io.substrait.proto.NamedStruct toProto(TypeProtoConverter typeProtoConverter) {
    var type = struct().accept(typeProtoConverter);
    return io.substrait.proto.NamedStruct.newBuilder()
        .setStruct(type.getStruct())
        .addAllNames(names())
        .build();
  }

  static io.substrait.type.NamedStruct convertNamedStructProtoToPojo(
      io.substrait.proto.NamedStruct namedStruct, ProtoTypeConverter protoTypeConverter) {
    var struct = namedStruct.getStruct();
    return ImmutableNamedStruct.builder()
        .names(namedStruct.getNamesList())
        .struct(
            Type.Struct.builder()
                .fields(
                    struct.getTypesList().stream()
                        .map(protoTypeConverter::from)
                        .collect(java.util.stream.Collectors.toList()))
                .nullable(ProtoTypeConverter.isNullable(struct.getNullability()))
                .build())
        .build();
  }
}
