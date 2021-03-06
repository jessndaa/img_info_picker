class DateTimeUtility {
  static DateTime? getDateFromString(String? dateText) => dateText == null ? null : DateTime.parse(dateText).isUtc ? DateTime.parse(dateText) : DateTime.parse(dateText+ "Z");
  static DateTime? getDateFromStringMillisencode(String? dateText) =>  DateTime.fromMillisecondsSinceEpoch(int.tryParse(dateText ?? "0") ?? 0);
}