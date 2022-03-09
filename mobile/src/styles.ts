import { StyleSheet } from "react-native";

export const Colors = {
  white: "#ffffff",
  main: "#000000",
  activeGreen: "#00cc54",
  text: "#000000",
  //   mainLight: '#',
} as const;

export const FontStyles = StyleSheet.create({
  headline: {
    fontFamily: "QuicksandBold",
    fontSize: 26,
    // lineHeight: 37,
    letterSpacing: 0.2,
    color: Colors.main,
  } as const,
  mainButtonText: {
    fontFamily: "AsapSemibold",
    fontSize: 18,
    color: Colors.white,
  } as const,
  mainRedLabel: {
    fontSize: 14,
    fontFamily: "AsapSemibold",
    color: Colors.main,
  } as const,
  mainLabel16: {
    fontSize: 17,
    fontFamily: "AsapSemibold",
    color: Colors.main,
    letterSpacing: 0.2,
  } as const,
  basicDesc: {
    fontSize: 16,
    fontFamily: "AsapRegular",
    color: Colors.text,
  } as const,
  medium16: {
    fontSize: 16,
    fontFamily: "AsapMedium",
    color: Colors.text,
  } as const,
  semiboldDesc: {
    fontSize: 14,
    fontFamily: "AsapSemibold",
    color: Colors.text,
  } as const,
  mediumDesc: {
    fontSize: 14,
    fontFamily: "AsapMedium",
    color: Colors.text,
  } as const,
  regular13: {
    fontSize: 13,
    fontFamily: "AsapRegular",
    color: Colors.text,
  } as const,
});

export const FlexStyles = StyleSheet.create({
  row: {
    flexDirection: "row",
  },
  center: {
    justifyContent: "center",
    alignItems: "center",
  },
  rowAlignCenter: {
    flexDirection: "row",
    alignItems: "center",
  },
  rowCenter: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
  },
  rowSpaceBetween: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  rowSpaceEvenly: {
    flexDirection: "row",
    justifyContent: "space-evenly",
    alignItems: "center",
  },
  rowSpaceAround: {
    flexDirection: "row",
    justifyContent: "space-around",
    alignItems: "center",
  },
} as const);
