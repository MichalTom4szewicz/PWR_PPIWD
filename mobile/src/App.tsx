import React from "react";
import { StyleSheet, Text, View } from "react-native";
import { useFonts } from "expo-font";
import { StatusBar } from "expo-status-bar";

export const App = () => {
  let [fontsLoaded] = useFonts({
    QuicksandBold: require("./assets/fonts/Quicksand-Bold.ttf"),
    AsapBold: require("./assets/fonts/Asap-Bold.ttf"),
    AsapSemibold: require("./assets/fonts/Asap-Bold.ttf"),
    AsapMedium: require("./assets/fonts/Asap-Medium.ttf"),
    AsapRegular: require("./assets/fonts/Asap-Regular.ttf"),
  });

  if (!fontsLoaded) {
    return null;
  }

  return (
    <View style={styles.container}>
      <Text>Open up App.tsx to start working on your app!</Text>
      <StatusBar style="auto" />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
  },
});
