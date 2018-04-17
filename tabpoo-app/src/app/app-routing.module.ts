import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LogComponent} from './LogEntry/log/log.component';

const routes: Routes = [
  { path: 'LogEntry', component: LogComponent }
];

@NgModule({
  exports: [ RouterModule ],
  imports: [ RouterModule.forRoot(routes) ],
})
export class AppRoutingModule {}