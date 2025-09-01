//
//  ContentView.swift
//  sampleIosApp
//
//  Created by Lucas Yuji Yoshimine on 29/08/2025.
//

import SwiftUI
   
struct ContentView: View {
    var body: some View {
        CameraView().ignoresSafeArea(.keyboard)
    }
}

#Preview {
    ContentView()
}
