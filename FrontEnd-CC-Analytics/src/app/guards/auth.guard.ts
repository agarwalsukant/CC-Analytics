import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthGuard implements CanActivate {
    
    constructor(private authservice: AuthService,private router: Router){}

    canActivate():boolean{
        if (this.authservice.loggedIn()){
            return true;
        } else {
            this.router.navigate(['/login.html']);
            return false;
        }
    }
}