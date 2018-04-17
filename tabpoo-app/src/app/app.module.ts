import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {HttpClientModule} from '@angular/common/http';


import { AppComponent } from './app.component';
import { LogComponent } from './LogEntry/log/log.component';
import { AppRoutingModule } from './/app-routing.module';
import { BristolInputComponent } from './LogEntry/bristol-input/bristol-input.component';


@NgModule({
  declarations: [
    AppComponent,
    LogComponent,
    BristolInputComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
