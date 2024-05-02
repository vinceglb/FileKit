//
//  ContentView.swift
//  macOSApp
//
//  Created by Vincent Guillebaud on 04/04/2024.
//

import SwiftUI
import SamplePickerKt

struct ContentView: View {
    let viewModel = MainViewModel()
    
    @State
    var uiState: MainUiState = MainUiState()
    
    var body: some View {
        // Convert Set to Array
        let files = Array(uiState.files)
        
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text("Hello, world!")
            
            Button("Single image picker") {
                viewModel.pickImage()
            }
            
            Button("Multiple images picker") {
                viewModel.pickImages()
            }
            
            Button("Single file picker, only png") {
                viewModel.pickFile()
            }
            
            Button("Multiple file picker, only png") {
                viewModel.pickFiles()
            }
            
            Button("Directory picker") {
                viewModel.pickDirectory()
            }
            
            if uiState.loading {
                ProgressView()
            }
            
            Text("Directory: \(String(describing: uiState.directory?.path))")
            
            List(files, id: \.nsUrl) { file in
                Text(file.name)
                    .onTapGesture { viewModel.saveFile(file: file) }
            }
        }
        .padding()
        .task {
            for await state in viewModel.uiState {
                uiState = state
            }
        }
    }
}

#Preview {
    ContentView()
}
